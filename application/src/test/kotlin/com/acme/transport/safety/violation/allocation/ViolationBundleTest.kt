package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationBundle("code-604", "name-521")
        assertEquals(ViolationBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationBundle("code-604", "name-521")
        try {
            item.moveTo(ViolationBundleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationBundleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationBundle("code-604", "name-521")
        item.status = ViolationBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationBundleStatus.REQUESTED) }
    }
}
