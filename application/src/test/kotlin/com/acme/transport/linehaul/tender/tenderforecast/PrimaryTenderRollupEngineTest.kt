package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderRollupEngine(newRegistry())
        val item = newPrimaryTenderRollup("code-868", "name-811")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
