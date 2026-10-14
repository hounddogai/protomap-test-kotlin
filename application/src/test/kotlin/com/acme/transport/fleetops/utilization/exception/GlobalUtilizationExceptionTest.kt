package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalUtilizationExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalUtilizationException("code-487", "name-386")
        assertEquals(GlobalUtilizationExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newGlobalUtilizationException("code-487", "name-386")
        try {
            item.moveTo(GlobalUtilizationExceptionStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalUtilizationExceptionStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalUtilizationException("code-487", "name-386")
        item.status = GlobalUtilizationExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalUtilizationExceptionStatus.OPEN) }
    }
}
