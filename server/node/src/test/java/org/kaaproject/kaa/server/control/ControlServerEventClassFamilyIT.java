/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyVersionDto;

/**
 * The Class ControlServerEventClassFamilyIT.
 */
public class ControlServerEventClassFamilyIT extends AbstractTestControlServer {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantAdminNeeded()
     */
    @Override
    protected boolean createTenantAdminNeeded() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantDeveloperNeeded()
     */
    @Override
    protected boolean createTenantDeveloperNeeded() {
        return false;
    }
    
    /**
     * Test create event class family.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateEventClassFamily() throws Exception {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        Assert.assertFalse(strIsEmpty(eventClassFamily.getId()));
    }
    
    /**
     * Test get event class family.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEventClassFamily() throws Exception {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        
        EventClassFamilyDto storedEventClassFamily = client.getEventClassFamilyById(eventClassFamily.getId());
        
        Assert.assertNotNull(storedEventClassFamily);
        assertEventClassFamiliesEquals(eventClassFamily, storedEventClassFamily);
    }
    
    /**
     * Test get event class families by tenant id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEventClassFamiliesByTenantId() throws Exception {
        List<EventClassFamilyDto> eventClassFamilies  = new ArrayList<EventClassFamilyDto>(10);
        tenantAdminDto = createTenant(tenantAdminUser);
        loginTenantAdmin(tenantAdminUser);
        for (int i=0;i<10;i++) {
            EventClassFamilyDto eventClassFamily = createEventClassFamily(tenantAdminDto.getId(), ""+i);
            eventClassFamilies.add(eventClassFamily);
        }
        
        Collections.sort(eventClassFamilies, new IdComparator());
        
        List<EventClassFamilyDto> storedEventClassFamilies = client.getEventClassFamilies();

        Collections.sort(storedEventClassFamilies, new IdComparator());
        
        Assert.assertEquals(eventClassFamilies.size(), storedEventClassFamilies.size());
        for (int i=0;i<eventClassFamilies.size();i++) {
            EventClassFamilyDto eventClassFamily = eventClassFamilies.get(i);
            EventClassFamilyDto storedEventClassFamily = storedEventClassFamilies.get(i);
            assertEventClassFamiliesEquals(eventClassFamily, storedEventClassFamily);
        }
    }
    
    /**
     * Test update event class family.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateEventClassFamily() throws Exception {
        EventClassFamilyDto eventClassFamily = createEventClassFamily();
        eventClassFamily.setName(generateString(EVENT_CLASS_FAMILY));
        EventClassFamilyDto updatedEventClassFamily = client
                .editEventClassFamily(eventClassFamily);
        assertEventClassFamiliesEquals(updatedEventClassFamily, eventClassFamily);
    }
    
    /**
     * Test get event classes by family id version and type.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEventClassesByFamilyIdVersionAndType() throws Exception {
        tenantAdminDto = createTenant(tenantAdminUser);
        loginTenantAdmin(tenantAdminUser);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenantAdminDto.getId());
        EventClassFamilyVersionDto eventClassFamilyVersion = createEventClassFamilyVersion(eventClassFamily.getId());
        client.addEventClassFamilyVersion(eventClassFamily.getId(), eventClassFamilyVersion);
        List<EventClassDto> eventClasses = client.getEventClassesByFamilyIdVersionAndType(eventClassFamily.getId(), 1, EventClassType.EVENT);
        Assert.assertNotNull(eventClasses);
        Assert.assertEquals(1, eventClasses.size());
        for (EventClassDto eventClass : eventClasses) {
            Assert.assertEquals(eventClassFamily.getId(), eventClass.getEcfId());
            Assert.assertEquals(0, eventClass.getVersion());
        }
    }
    
    /**
     * Test duplicate event class family name.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDuplicateEventClassFamilyName() throws Exception {
        TenantUserDto tenant = createTenant(tenantAdminUser);
        loginTenantAdmin(tenantAdminUser);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenant.getId());
        final EventClassFamilyDto secondEventClassFamily = createEventClassFamily(tenant.getId(), "test");
        secondEventClassFamily.setName(eventClassFamily.getName());
        checkBadRequest(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.editEventClassFamily(secondEventClassFamily);
                
            }
        });
    }

    /**
     * Test add event class family schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAddEventClassFamilyVersion() throws Exception {
        tenantAdminDto = createTenant(tenantAdminUser);
        loginTenantAdmin(tenantAdminUser);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenantAdminDto.getId());
        EventClassFamilyVersionDto eventClassFamilyVersion = createEventClassFamilyVersion(eventClassFamily.getId());
        client.addEventClassFamilyVersion(eventClassFamily.getId(), eventClassFamilyVersion);
        List<EventClassFamilyVersionDto> schemas = eventClassService.findEventClassFamilyVersionsById(eventClassFamily.getId());
        Assert.assertNotNull(schemas);
        Assert.assertEquals(1, schemas.size());
        EventClassFamilyVersionDto eventSchema = schemas.get(0);
        Assert.assertNotNull(eventSchema);
        Assert.assertEquals(1, eventSchema.getVersion());

        client.addEventClassFamilyVersion(eventClassFamily.getId(), eventClassFamilyVersion);
        schemas = eventClassService.findEventClassFamilyVersionsById(eventClassFamily.getId());
        Assert.assertNotNull(schemas);
        Assert.assertEquals(2, schemas.size());
        eventSchema = schemas.get(1);
        Assert.assertNotNull(eventSchema);
        Assert.assertEquals(2, eventSchema.getVersion());
    }

    /**
     * Test duplicate event class family fqns.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDuplicateEventClassFamilyFqns() throws Exception {
        tenantAdminDto = createTenant(tenantAdminUser);
        loginTenantAdmin(tenantAdminUser);
        EventClassFamilyDto eventClassFamily = createEventClassFamily(tenantAdminDto.getId());
        final EventClassFamilyVersionDto eventClassFamilyVersion = createEventClassFamilyVersion(eventClassFamily.getId());
        loginTenantAdmin(tenantAdminUser);
        client.addEventClassFamilyVersion(eventClassFamily.getId(), eventClassFamilyVersion);
        final EventClassFamilyDto secondEventClassFamily = createEventClassFamily(tenantAdminDto.getId(), "test");
        checkBadRequest(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.addEventClassFamilyVersion(secondEventClassFamily.getId(), eventClassFamilyVersion);
            }
        });
    }
    
    /**
     * Assert event class families equals.
     *
     * @param eventClassFamily the event class family
     * @param storedEventClassFamily the stored event class family
     */
    private void assertEventClassFamiliesEquals(EventClassFamilyDto eventClassFamily, EventClassFamilyDto storedEventClassFamily) {
        Assert.assertEquals(eventClassFamily.getId(), storedEventClassFamily.getId());
        Assert.assertEquals(eventClassFamily.getName(), storedEventClassFamily.getName());
        Assert.assertEquals(eventClassFamily.getTenantId(), storedEventClassFamily.getTenantId());
    }

}
