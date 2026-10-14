package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventRollupEngine(newRegistry())
        val item = newCameraEventRollup("code-145", "label-952")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
