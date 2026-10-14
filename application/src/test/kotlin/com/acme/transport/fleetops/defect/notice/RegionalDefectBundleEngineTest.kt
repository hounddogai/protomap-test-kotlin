package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDefectBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDefectBundleEngine(newRegistry())
        val item = newRegionalDefectBundle("code-246", "label-786")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
