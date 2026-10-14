package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newServiceAreaAdjustment("code-133", "title-578")
        assertEquals(ServiceAreaAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newServiceAreaAdjustment("code-133", "title-578")
        try {
            item.moveTo(ServiceAreaAdjustmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaAdjustmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServiceAreaAdjustment("code-133", "title-578")
        item.status = ServiceAreaAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(ServiceAreaAdjustmentStatus.NEW) }
    }
}
