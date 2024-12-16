package org.esupportail.scim.bash;


import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.client.rest.ScimGroupClient;
import org.apache.directory.scim.client.rest.ScimJacksonXmlBindJsonProvider;
import org.apache.directory.scim.client.rest.ScimUserClient;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schemas;
import org.esupportail.scim.diff.PatchGenerator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.slf4j.Logger;

import java.util.*;

public class ScimClientFakeUsers {

    final static Logger log = org.slf4j.LoggerFactory.getLogger(ScimClientFakeUsers.class);

    final static String SCIM_URL = "http://localhost:8080/scim2";

    static Map<String, ScimGroup> fakedGroups = new HashMap<>();

    static Map<String, ScimUser> fakedUsers = new HashMap<>();

    public static void main(String[] args) throws Exception {
        preloadFakedData();

        SchemaRegistry schemaRegistry = new SchemaRegistry();
        schemaRegistry.addSchema(ScimUser.class, null);
        schemaRegistry.addSchema(ScimGroup.class, null);

        HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basic("scim", "esup");
        Client client = ClientBuilder.newBuilder()
                .withConfig( // the default Jersey client does not support PATCH requests
                        new ClientConfig().connectorProvider(new JdkConnectorProvider()))
                .register(new ScimJacksonXmlBindJsonProvider(schemaRegistry))
                .register(basicAuthFeature)
                .build();
        ScimGroupClient scimGroupClient = new ScimGroupClient(client, SCIM_URL);

        ListResponse<ScimGroup> listResponseGroups = scimGroupClient.query(null, null, null, null, null, null, null);
        log.info("Groups on server : {}", listResponseGroups);

        for(ScimGroup group : fakedGroups.values()) {
            if(listResponseGroups.getTotalResults()==0 || listResponseGroups.getResources().stream().noneMatch(g -> g.getId().equals(group.getId()))) {
                ScimGroup createdGroup = scimGroupClient.create(group);
                log.info("Created group : {}", createdGroup);
            }
        }

        ScimUserClient scimUserClient = new ScimUserClient(client, SCIM_URL);
        ListResponse<ScimUser> listResponseUsers = scimUserClient.query(null, null, null, null, null, null, null);
        log.info("Users on server : {}", listResponseGroups);
        for(ScimUser user : fakedUsers.values()) {
            if(listResponseUsers.getTotalResults()==0 || listResponseUsers.getResources().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
                ScimUser createdUser = scimUserClient.create(user);
                log.info("Created user : {}", createdUser);
            }
        }

        for(ScimGroup fakedGroup : fakedGroups.values()) {
            Optional<ScimGroup> scimGroup = scimGroupClient.getById(fakedGroup.getId());
            if(scimGroup.isPresent()) {
                PatchGenerator patchGenerator = new PatchGenerator(schemaRegistry);
                List<PatchOperation> patchOperations = patchGenerator.diff(scimGroup.get(), fakedGroup);
                if(!patchOperations.isEmpty()) {
                    PatchRequest patchRequest = new PatchRequest();
                    patchRequest.setPatchOperationList(patchOperations);
                    ScimGroup updatedGroup = scimGroupClient.patch(fakedGroup.getId(), patchRequest);
                    log.info("Updated group : {}", updatedGroup);
                }
            }
        }

    }

    public static void preloadFakedData() {
        Faker faker = new Faker();
        for(int i=0; i < 5; i++) {
            ScimUser user = new ScimUser();
            Name fakedName = faker.name();
            user.setId(StringUtils.lowerCase(fakedName.lastName() + "@example.org"));
            user.setUserName(fakedName.username() + "@example.org");
            user.setDisplayName(fakedName.fullName());
            String emailAdr = String.format("%s.%s@example.org", fakedName.firstName(), fakedName.lastName());
            Email email = new Email();
            email.setValue(emailAdr);
            email.setPrimary(true);
            user.setEmails(List.of(email));
            fakedUsers.put(user.getId(), user);
        }
        for(int i=0; i < 2; i++) {
            ScimGroup group = new ScimGroup();
            group.setId("fakedGroup" + i);
            group.setDisplayName("Faked Group " + i + " with " + (i+2) + " members");
            List<String> usersInRandomOrder = new ArrayList<>(fakedUsers.keySet());
            Collections.shuffle(usersInRandomOrder);
            List<GroupMembership> members = new ArrayList<>();
            for(int j=0; j< i+2; j++) {
                GroupMembership member = new GroupMembership();
                member.setValue(usersInRandomOrder.get(j));
                members.add(member);
            }
            group.setMembers(members);
            fakedGroups.put(group.getId(), group);
        }
    }

}
