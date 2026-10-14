package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultZoneCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultZoneCycleEngine(newRegistry())
        val item = newDefaultZoneCycle("code-802", "title-910")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
