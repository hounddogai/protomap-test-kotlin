package com.acme.transport.fleetops.utilization.utilizationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalUtilizationOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalUtilizationOverride("code-996", "name-797")
        assertEquals(RegionalUtilizationOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalUtilizationOverride("code-996", "name-797")
        try {
            item.moveTo(RegionalUtilizationOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalUtilizationOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalUtilizationOverride("code-996", "name-797")
        item.status = RegionalUtilizationOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalUtilizationOverrideStatus.CREATED) }
    }
}
