package com.acme.transport.customs.hold.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newHoldOverride("code-483", "title-997")
        assertEquals(HoldOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newHoldOverride("code-483", "title-997")
        try {
            item.moveTo(HoldOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHoldOverride("code-483", "title-997")
        item.status = HoldOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(HoldOverrideStatus.CREATED) }
    }
}
