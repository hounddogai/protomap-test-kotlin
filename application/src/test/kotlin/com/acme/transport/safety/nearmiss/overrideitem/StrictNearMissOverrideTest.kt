package com.acme.transport.safety.nearmiss.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNearMissOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newStrictNearMissOverride("code-236", "name-627")
        assertEquals(StrictNearMissOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictNearMissOverride("code-236", "name-627")
        try {
            item.moveTo(StrictNearMissOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNearMissOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictNearMissOverride("code-236", "name-627")
        item.status = StrictNearMissOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(StrictNearMissOverrideStatus.CREATED) }
    }
}
