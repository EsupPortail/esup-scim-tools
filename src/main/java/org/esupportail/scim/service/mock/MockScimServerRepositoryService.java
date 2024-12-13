package org.esupportail.scim.service.mock;

import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.GroupMembership;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.esupportail.scim.service.ScimServerRepositoryService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MockScimServerRepositoryService implements ScimServerRepositoryService {

    final Logger log = org.slf4j.LoggerFactory.getLogger(MockScimServerRepositoryService.class);

    Map<String, ScimUser> scimUsers = new HashMap<>();

    Map<String, ScimGroup> scimGroups = new HashMap<>();

    @Override
    public void createUser(ScimUser scimUser) {
        scimUsers.put(scimUser.getId(), scimUser);
    }

    @Override
    public void updateUser(String id, ScimUser scimUser) {
        scimUsers.put(id, scimUser);
    }

    @Override
    public void patchUser(String id, List<PatchOperation> patchOperations) {
        // TODO
    }

    @Override
    public ScimUser getUser(String id) {
        return scimUsers.get(id);
    }

    @Override
    public List<ScimUser> findUsers(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
        return List.copyOf(scimUsers.values());
    }

    @Override
    public void deleteUser(String id) {
        scimUsers.remove(id);
    }

    @Override
    public void createGroup(ScimGroup scimGroup) {
        scimGroups.put(scimGroup.getId(), scimGroup);
    }

    @Override
    public void updateGroup(String id, ScimGroup scimGroup) {
        scimGroups.put(id, scimGroup);
    }

    @Override
    public void patchGroup(String id, List<PatchOperation> patchOperations) {
        ScimGroup group = scimGroups.get(id);
        for(PatchOperation patchOperation : patchOperations) {
            log.debug("Patch operation: " + patchOperation);
            if("members".equals(patchOperation.getPath().toString())) {
                if(PatchOperation.Type.ADD.equals(patchOperation.getOperation())) {
                    for(Map<String, String> memberMap : (List<Map<String, String>>)patchOperation.getValue()) {
                        String eppn = memberMap.get("value");
                        GroupMembership groupMembership = new GroupMembership();
                        groupMembership.setValue(eppn);
                        group.addMember(groupMembership);
                    }
                }
                if(PatchOperation.Type.REMOVE.equals(patchOperation.getOperation())) {
                    for(Map<String, String> memberMap : (List<Map<String, String>>)patchOperation.getValue()) {
                        String eppn = memberMap.get("value");
                        group.getMembers().removeIf(member -> eppn.equals(member.getValue()));
                    }
                }
            }
        }
    }

    @Override
    public ScimGroup getGroup(String id) {
        return scimGroups.get(id);
    }

    @Override
    public List<ScimGroup> findGroups(Filter filter, PageRequest pageRequest, SortRequest sortRequest) {
        return List.copyOf(scimGroups.values());
    }

    @Override
    public void deleteGroup(String id) {
        scimGroups.remove(id);
    }
}
