package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCameraEventRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCameraEventRollupEngine(newRegistry())
        val item = newTieredCameraEventRollup("code-836", "label-430")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
