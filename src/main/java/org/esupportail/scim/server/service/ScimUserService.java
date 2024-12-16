package org.esupportail.scim.server.service;

import org.apache.directory.scim.core.repository.InvalidRepositoryException;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.FilterResponse;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ScimUserService implements Repository<ScimUser> {

    private static final Logger log = LoggerFactory.getLogger(ScimUserService.class);

    private final ScimServerRepositoryService scimServerRepositoryService;

    public ScimUserService(ScimServerRepositoryService scimServerRepositoryService) {
        this.scimServerRepositoryService = scimServerRepositoryService;
    }

    @Override
    public Class<ScimUser> getResourceClass() {
        return ScimUser.class;
    }

    @Override
    public ScimUser create(ScimUser scimUser) throws ResourceException {
        log.info("Creating User: " + scimUser);
        scimServerRepositoryService.createUser(scimUser);
        return scimUser;
    }

    @Override
    public ScimUser update(String id, String version, ScimUser ScimUser, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
        log.info("Should Updating User ...: " + id + " with User: " + ScimUser);
        scimServerRepositoryService.updateUser(id, ScimUser);
        return get(id);
    }

    @Override
    public ScimUser patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
        log.info("Should patch User ...: " + id + " with patch operations: " + patchOperations);
        scimServerRepositoryService.patchUser(id, patchOperations);
        return get(id);
    }

    @Override
    public ScimUser get(String id) throws ResourceException {
        log.info("Getting User: " + id);
        return scimServerRepositoryService.getUser(id);
    }

    @Override
    public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
        log.info("Finding Users with filter {}", filter);
        List<ScimUser> scimUsers = scimServerRepositoryService.findUsers(filter, pageRequest, sortRequest);
        log.info("-> {} Users found", scimUsers.size());
        return new FilterResponse<>(scimUsers, pageRequest, scimUsers.size());
    }

    @Override
    public void delete(String id) throws ResourceException {
        log.info("Deleting User: " + id);
        scimServerRepositoryService.deleteUser(id);
    }

    @Override
    public List<Class<? extends ScimExtension>> getExtensionList() throws InvalidRepositoryException {
        return Repository.super.getExtensionList();
    }
}
