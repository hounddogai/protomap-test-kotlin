package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling backhaul templates by code. */
interface RollingBackhaulTemplateStore {
    fun find(code: String): RollingBackhaulTemplate?
    fun save(item: RollingBackhaulTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBackhaulTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBackhaulTemplateStore(
    var items: MutableMap<String, RollingBackhaulTemplate>,
    var order: MutableList<String>,
) : RollingBackhaulTemplateStore {
    override fun find(code: String): RollingBackhaulTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBackhaulTemplate) {
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

    override fun list(): MutableList<RollingBackhaulTemplate> {
        val out = mutableListOf<RollingBackhaulTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBackhaulTemplateStore(): RollingBackhaulTemplateStore {
    return MemoryRollingBackhaulTemplateStore(
        mutableMapOf<String, RollingBackhaulTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBackhaulTemplateStore(
    var inner: RollingBackhaulTemplateStore,
    var cache: TtlCache<RollingBackhaulTemplate>,
    var hits: Int,
    var misses: Int,
) : RollingBackhaulTemplateStore {
    override fun find(code: String): RollingBackhaulTemplate? {
        val hit = (cache.get(code) as RollingBackhaulTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBackhaulTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBackhaulTemplate> = inner.list()
}

fun newCachedRollingBackhaulTemplateStore(inner: RollingBackhaulTemplateStore, ttl: Int, clock: Clock): RollingBackhaulTemplateStore {
    return CachedRollingBackhaulTemplateStore(
        inner = inner,
        cache = newTtlCache<RollingBackhaulTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling backhaul templates. Empty fields match all. */
data class RollingBackhaulTemplateQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBackhaulTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBackhaulTemplate>): MutableList<RollingBackhaulTemplate> {
        val matched = filter(items) { candidate: RollingBackhaulTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBackhaulTemplates(store: RollingBackhaulTemplateStore, query: RollingBackhaulTemplateQuery): MutableList<RollingBackhaulTemplate> {
    return query.apply(store.list())
}
