package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellQuotaEngine(newRegistry())
        val item = newDwellQuota("code-869", "label-315")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
