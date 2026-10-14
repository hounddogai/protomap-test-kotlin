package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodManifestEngine(newRegistry())
        val item = newDutyPeriodManifest("code-489", "name-818")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
