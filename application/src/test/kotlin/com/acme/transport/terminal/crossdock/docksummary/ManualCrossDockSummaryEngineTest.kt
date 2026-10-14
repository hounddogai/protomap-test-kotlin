package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCrossDockSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCrossDockSummaryEngine(newRegistry())
        val item = newManualCrossDockSummary("code-244", "name-364")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
