package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryHoldOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryHoldOverride("code-784", "title-381")
        assertEquals(PrimaryHoldOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryHoldOverride("code-784", "title-381")
        try {
            item.moveTo(PrimaryHoldOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryHoldOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryHoldOverride("code-784", "title-381")
        item.status = PrimaryHoldOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryHoldOverrideStatus.CREATED) }
    }
}
