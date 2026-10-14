package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryServiceAreaEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryServiceAreaEntryEngine(newRegistry())
        val item = newPrimaryServiceAreaEntry("code-962", "label-673")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
