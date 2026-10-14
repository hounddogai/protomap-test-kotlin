package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAccessorialRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAccessorialRollupEngine(newRegistry())
        val item = newDefaultAccessorialRollup("code-281", "name-983")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
