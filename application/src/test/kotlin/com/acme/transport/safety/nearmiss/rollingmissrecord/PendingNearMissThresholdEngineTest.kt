package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingNearMissThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingNearMissThresholdEngine(newRegistry())
        val item = newPendingNearMissThreshold("code-321", "name-904")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
