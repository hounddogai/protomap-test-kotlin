package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newNearMissPolicy("code-728", "name-37")
        assertEquals(NearMissPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newNearMissPolicy("code-728", "name-37")
        try {
            item.moveTo(NearMissPolicyStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissPolicyStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNearMissPolicy("code-728", "name-37")
        item.status = NearMissPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(NearMissPolicyStatus.CREATED) }
    }
}
