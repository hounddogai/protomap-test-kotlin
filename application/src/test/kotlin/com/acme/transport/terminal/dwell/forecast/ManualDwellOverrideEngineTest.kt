package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDwellOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDwellOverrideEngine(newRegistry())
        val item = newManualDwellOverride("code-695", "name-99")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
