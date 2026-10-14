package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingTariffCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingTariffCheckpoint("code-807", "title-381")
        val view = toRollingTariffCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingTariffCheckpointViews(mutableListOf<RollingTariffCheckpoint>(item, item)).size, "views")
    }
}
