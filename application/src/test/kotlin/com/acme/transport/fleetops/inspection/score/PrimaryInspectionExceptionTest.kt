package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryInspectionException("code-114", "label-578")
        assertEquals(PrimaryInspectionExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPrimaryInspectionException("code-114", "label-578")
        try {
            item.moveTo(PrimaryInspectionExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryInspectionException("code-114", "label-578")
        item.status = PrimaryInspectionExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryInspectionExceptionStatus.CREATED) }
    }
}
