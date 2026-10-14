package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationProfileTest {
    @Test
    fun `starts in open`() {
        val item = newViolationProfile("code-625", "label-815")
        assertEquals(ViolationProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newViolationProfile("code-625", "label-815")
        try {
            item.moveTo(ViolationProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newViolationProfile("code-625", "label-815")
        item.status = ViolationProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(ViolationProfileStatus.OPEN) }
    }
}
