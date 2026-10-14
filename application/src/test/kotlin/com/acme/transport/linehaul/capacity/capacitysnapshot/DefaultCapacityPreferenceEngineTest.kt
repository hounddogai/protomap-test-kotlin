package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCapacityPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCapacityPreferenceEngine(newRegistry())
        val item = newDefaultCapacityPreference("code-766", "title-981")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
