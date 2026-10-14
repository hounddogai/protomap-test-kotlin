package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackHoldPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackHoldPolicy("code-130", "title-763")
        assertEquals(FallbackHoldPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFallbackHoldPolicy("code-130", "title-763")
        try {
            item.moveTo(FallbackHoldPolicyStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackHoldPolicyStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackHoldPolicy("code-130", "title-763")
        item.status = FallbackHoldPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackHoldPolicyStatus.CREATED) }
    }
}
