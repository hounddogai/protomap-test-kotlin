package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newViolationClaim("code-166", "title-500")
        assertEquals(ViolationClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newViolationClaim("code-166", "title-500")
        try {
            item.moveTo(ViolationClaimStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationClaimStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newViolationClaim("code-166", "title-500")
        item.status = ViolationClaimStatus.STOPPED
        assertFails("move") { item.moveTo(ViolationClaimStatus.IDLE) }
    }
}
