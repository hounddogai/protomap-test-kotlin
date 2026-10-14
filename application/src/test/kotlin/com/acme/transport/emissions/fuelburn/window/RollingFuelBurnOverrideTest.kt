package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingFuelBurnOverride("code-21", "name-190")
        assertEquals(RollingFuelBurnOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRollingFuelBurnOverride("code-21", "name-190")
        try {
            item.moveTo(RollingFuelBurnOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingFuelBurnOverride("code-21", "name-190")
        item.status = RollingFuelBurnOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingFuelBurnOverrideStatus.PROPOSED) }
    }
}
