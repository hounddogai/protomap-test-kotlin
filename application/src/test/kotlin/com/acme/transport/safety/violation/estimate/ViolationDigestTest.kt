package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newViolationDigest("code-743", "name-385")
        assertEquals(ViolationDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newViolationDigest("code-743", "name-385")
        try {
            item.moveTo(ViolationDigestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationDigestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newViolationDigest("code-743", "name-385")
        item.status = ViolationDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(ViolationDigestStatus.PLANNED) }
    }
}
