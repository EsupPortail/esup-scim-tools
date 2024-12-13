package org.esupportail.scim.service;

import org.apache.directory.scim.spec.filter.Filter;
import org.apache.directory.scim.spec.filter.PageRequest;
import org.apache.directory.scim.spec.filter.SortRequest;
import org.apache.directory.scim.spec.patch.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;

import java.util.List;

public interface ScimServerRepositoryService {

    void createUser(ScimUser scimUser);

    void updateUser(String id, ScimUser scimUser);

    void patchUser(String id, List<PatchOperation> patchOperations);

    ScimUser getUser(String id);

    List<ScimUser> findUsers(Filter filter, PageRequest pageRequest, SortRequest sortRequest);

    void deleteUser(String id);

    void createGroup(ScimGroup scimGroup);

    void updateGroup(String id, ScimGroup scimGroup);

    void patchGroup(String id, List<PatchOperation> patchOperations);

    ScimGroup getGroup(String id);

    List<ScimGroup> findGroups(Filter filter, PageRequest pageRequest, SortRequest sortRequest);

    void deleteGroup(String id);

}
