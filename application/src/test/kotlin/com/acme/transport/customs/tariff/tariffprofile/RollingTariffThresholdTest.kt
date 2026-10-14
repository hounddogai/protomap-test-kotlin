package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTariffThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTariffThreshold("code-431", "label-374")
        assertEquals(RollingTariffThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingTariffThreshold("code-431", "label-374")
        try {
            item.moveTo(RollingTariffThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTariffThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTariffThreshold("code-431", "label-374")
        item.status = RollingTariffThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTariffThresholdStatus.CREATED) }
    }
}
