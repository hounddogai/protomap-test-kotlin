package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDefectExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingDefectException("code-599", "title-391")
        assertEquals(RollingDefectExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingDefectException("code-599", "title-391")
        try {
            item.moveTo(RollingDefectExceptionStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDefectExceptionStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingDefectException("code-599", "title-391")
        item.status = RollingDefectExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingDefectExceptionStatus.PLANNED) }
    }
}
