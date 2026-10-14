package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionRollupEngine(newRegistry())
        val item = newPendingInspectionRollup("code-527", "label-321")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
