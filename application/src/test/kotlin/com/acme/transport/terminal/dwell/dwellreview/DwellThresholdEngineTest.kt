package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellThresholdEngine(newRegistry())
        val item = newDwellThreshold("code-641", "label-555")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
