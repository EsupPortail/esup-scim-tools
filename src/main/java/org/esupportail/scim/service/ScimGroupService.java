package org.esupportail.scim.service;

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
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ScimGroupService implements Repository<ScimGroup> {

    private static final Logger log = LoggerFactory.getLogger(ScimGroupService.class);

    private final ScimServerRepositoryService scimServerRepositoryService;

    public ScimGroupService(ScimServerRepositoryService scimServerRepositoryService) {
        this.scimServerRepositoryService = scimServerRepositoryService;
    }

    @Override
    public Class<ScimGroup> getResourceClass() {
        return ScimGroup.class;
    }

    @Override
    public ScimGroup create(ScimGroup scimGroup) throws ResourceException {
        log.info("Creating group: " + scimGroup);
        scimServerRepositoryService.createGroup(scimGroup);
        return scimGroup;
    }

    @Override
    public ScimGroup update(String id, String version, ScimGroup ScimGroup, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
        log.info("Should Updating group ...: " + id + " with group: " + ScimGroup);
        scimServerRepositoryService.updateGroup(id, ScimGroup);
        return get(id);
    }

    @Override
    public ScimGroup patch(String id, String version, List<PatchOperation> patchOperations, Set<AttributeReference> includedAttributes, Set<AttributeReference> excludedAttributes) throws ResourceException {
        log.info("Should patch group ...: " + id + " with patch operations: " + patchOperations);
                log.info("Should patch group ...: " + id + " with patch operations: " + patchOperations);
        scimServerRepositoryService.patchGroup(id, patchOperations);
        return get(id);
    }

    @Override
    public ScimGroup get(String id) throws ResourceException {
        log.info("Getting group: " + id);
        return scimServerRepositoryService.getGroup(id);
    }

    @Override
    public FilterResponse<ScimGroup> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
        log.info("Finding groups with filter {}", filter);
        List<ScimGroup> scimGroups = scimServerRepositoryService.findGroups(filter, pageRequest, sortRequest);
        log.info("-> {} groups found", scimGroups.size());
        return new FilterResponse<>(scimGroups, pageRequest, scimGroups.size());
    }

    @Override
    public void delete(String id) throws ResourceException {
        log.info("Deleting group: " + id);
        scimServerRepositoryService.deleteGroup(id);
    }

    @Override
    public List<Class<? extends ScimExtension>> getExtensionList() throws InvalidRepositoryException {
        return Repository.super.getExtensionList();
    }
}
