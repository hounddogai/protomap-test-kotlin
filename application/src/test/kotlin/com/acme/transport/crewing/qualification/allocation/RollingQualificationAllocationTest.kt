package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingQualificationAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingQualificationAllocation("code-622", "title-345")
        assertEquals(RollingQualificationAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingQualificationAllocation("code-622", "title-345")
        try {
            item.moveTo(RollingQualificationAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingQualificationAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingQualificationAllocation("code-622", "title-345")
        item.status = RollingQualificationAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(RollingQualificationAllocationStatus.IDLE) }
    }
}
