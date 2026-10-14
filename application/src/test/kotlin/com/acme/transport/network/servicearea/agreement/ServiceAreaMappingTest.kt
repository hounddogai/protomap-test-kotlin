package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaMappingTest {
    @Test
    fun `starts in new`() {
        val item = newServiceAreaMapping("code-102", "name-686")
        assertEquals(ServiceAreaMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newServiceAreaMapping("code-102", "name-686")
        try {
            item.moveTo(ServiceAreaMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServiceAreaMapping("code-102", "name-686")
        item.status = ServiceAreaMappingStatus.FAILED
        assertFails("move") { item.moveTo(ServiceAreaMappingStatus.NEW) }
    }
}
