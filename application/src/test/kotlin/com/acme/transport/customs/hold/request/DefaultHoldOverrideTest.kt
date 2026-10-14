package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHoldOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultHoldOverride("code-244", "label-110")
        assertEquals(DefaultHoldOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultHoldOverride("code-244", "label-110")
        try {
            item.moveTo(DefaultHoldOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHoldOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultHoldOverride("code-244", "label-110")
        item.status = DefaultHoldOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultHoldOverrideStatus.OPEN) }
    }
}
