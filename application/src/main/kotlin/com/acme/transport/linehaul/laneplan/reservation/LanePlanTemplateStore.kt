package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan templates by code. */
interface LanePlanTemplateStore {
    fun find(code: String): LanePlanTemplate?
    fun save(item: LanePlanTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanTemplateStore(
    var items: MutableMap<String, LanePlanTemplate>,
    var order: MutableList<String>,
) : LanePlanTemplateStore {
    override fun find(code: String): LanePlanTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanTemplate) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LanePlanTemplate> {
        val out = mutableListOf<LanePlanTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanTemplateStore(): LanePlanTemplateStore {
    return MemoryLanePlanTemplateStore(mutableMapOf<String, LanePlanTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanTemplateStore(
    var inner: LanePlanTemplateStore,
    var cache: TtlCache<LanePlanTemplate>,
    var hits: Int,
    var misses: Int,
) : LanePlanTemplateStore {
    override fun find(code: String): LanePlanTemplate? {
        val hit = (cache.get(code) as LanePlanTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanTemplate> = inner.list()
}

fun newCachedLanePlanTemplateStore(inner: LanePlanTemplateStore, ttl: Int, clock: Clock): LanePlanTemplateStore {
    return CachedLanePlanTemplateStore(
        inner = inner,
        cache = newTtlCache<LanePlanTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan templates. Empty fields match all. */
data class LanePlanTemplateQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanTemplate>): MutableList<LanePlanTemplate> {
        val matched = filter(items) { candidate: LanePlanTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanTemplates(store: LanePlanTemplateStore, query: LanePlanTemplateQuery): MutableList<LanePlanTemplate> {
    return query.apply(store.list())
}
