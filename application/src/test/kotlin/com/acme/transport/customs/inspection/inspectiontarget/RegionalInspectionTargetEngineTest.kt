package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalInspectionTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalInspectionTargetEngine(newRegistry())
        val item = newRegionalInspectionTarget("code-266", "name-871")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
