package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDwellDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDwellDigestEngine(newRegistry())
        val item = newRegionalDwellDigest("code-577", "label-408")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
