package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionRollupEngine(newRegistry())
        val item = newInspectionRollup("code-372", "label-358")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
