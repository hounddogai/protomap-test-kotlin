package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling redelivery templates by code. */
interface RollingRedeliveryTemplateStore {
    fun find(code: String): RollingRedeliveryTemplate?
    fun save(item: RollingRedeliveryTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRedeliveryTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRedeliveryTemplateStore(
    var items: MutableMap<String, RollingRedeliveryTemplate>,
    var order: MutableList<String>,
) : RollingRedeliveryTemplateStore {
    override fun find(code: String): RollingRedeliveryTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRedeliveryTemplate) {
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

    override fun list(): MutableList<RollingRedeliveryTemplate> {
        val out = mutableListOf<RollingRedeliveryTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRedeliveryTemplateStore(): RollingRedeliveryTemplateStore {
    return MemoryRollingRedeliveryTemplateStore(
        mutableMapOf<String, RollingRedeliveryTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRedeliveryTemplateStore(
    var inner: RollingRedeliveryTemplateStore,
    var cache: TtlCache<RollingRedeliveryTemplate>,
    var hits: Int,
    var misses: Int,
) : RollingRedeliveryTemplateStore {
    override fun find(code: String): RollingRedeliveryTemplate? {
        val hit = (cache.get(code) as RollingRedeliveryTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRedeliveryTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRedeliveryTemplate> = inner.list()
}

fun newCachedRollingRedeliveryTemplateStore(inner: RollingRedeliveryTemplateStore, ttl: Int, clock: Clock): RollingRedeliveryTemplateStore {
    return CachedRollingRedeliveryTemplateStore(
        inner = inner,
        cache = newTtlCache<RollingRedeliveryTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling redelivery templates. Empty fields match all. */
data class RollingRedeliveryTemplateQuery(
    var lane: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRedeliveryTemplate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRedeliveryTemplate>): MutableList<RollingRedeliveryTemplate> {
        val matched = filter(items) { candidate: RollingRedeliveryTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRedeliveryTemplates(store: RollingRedeliveryTemplateStore, query: RollingRedeliveryTemplateQuery): MutableList<RollingRedeliveryTemplate> {
    return query.apply(store.list())
}
