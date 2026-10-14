package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelBurnScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalFuelBurnSchedule("code-459", "title-84")
        assertEquals(GlobalFuelBurnScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalFuelBurnSchedule("code-459", "title-84")
        try {
            item.moveTo(GlobalFuelBurnScheduleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelBurnScheduleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalFuelBurnSchedule("code-459", "title-84")
        item.status = GlobalFuelBurnScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalFuelBurnScheduleStatus.PROPOSED) }
    }
}
