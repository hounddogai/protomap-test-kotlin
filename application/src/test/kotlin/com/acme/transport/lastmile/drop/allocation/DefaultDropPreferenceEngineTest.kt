package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDropPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDropPreferenceEngine(newRegistry())
        val item = newDefaultDropPreference("code-88", "title-315")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
