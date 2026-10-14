package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newNearMissOverride("code-184", "label-911")
        assertEquals(NearMissOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newNearMissOverride("code-184", "label-911")
        try {
            item.moveTo(NearMissOverrideStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissOverrideStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNearMissOverride("code-184", "label-911")
        item.status = NearMissOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(NearMissOverrideStatus.CREATED) }
    }
}
