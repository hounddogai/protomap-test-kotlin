package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialWindowTest {
    @Test
    fun `starts in new`() {
        val item = newAccessorialWindow("code-117", "title-763")
        assertEquals(AccessorialWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAccessorialWindow("code-117", "title-763")
        try {
            item.moveTo(AccessorialWindowStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialWindowStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAccessorialWindow("code-117", "title-763")
        item.status = AccessorialWindowStatus.FAILED
        assertFails("move") { item.moveTo(AccessorialWindowStatus.NEW) }
    }
}
