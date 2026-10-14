package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newViolationGroup("code-280", "title-692")
        assertEquals(ViolationGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newViolationGroup("code-280", "title-692")
        try {
            item.moveTo(ViolationGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newViolationGroup("code-280", "title-692")
        item.status = ViolationGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ViolationGroupStatus.PROPOSED) }
    }
}
