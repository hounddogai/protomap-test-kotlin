package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialMappingTest {
    @Test
    fun `starts in new`() {
        val item = newAccessorialMapping("code-567", "label-451")
        assertEquals(AccessorialMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAccessorialMapping("code-567", "label-451")
        try {
            item.moveTo(AccessorialMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAccessorialMapping("code-567", "label-451")
        item.status = AccessorialMappingStatus.FAILED
        assertFails("move") { item.moveTo(AccessorialMappingStatus.NEW) }
    }
}
