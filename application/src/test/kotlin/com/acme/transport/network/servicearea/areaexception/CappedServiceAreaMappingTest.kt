package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServiceAreaMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedServiceAreaMapping("code-504", "label-356")
        assertEquals(CappedServiceAreaMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedServiceAreaMapping("code-504", "label-356")
        try {
            item.moveTo(CappedServiceAreaMappingStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServiceAreaMappingStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedServiceAreaMapping("code-504", "label-356")
        item.status = CappedServiceAreaMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedServiceAreaMappingStatus.REQUESTED) }
    }
}
