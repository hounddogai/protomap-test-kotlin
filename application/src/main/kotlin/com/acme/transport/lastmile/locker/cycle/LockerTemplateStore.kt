package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker templates by code. */
interface LockerTemplateStore {
    fun find(code: String): LockerTemplate?
    fun save(item: LockerTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerTemplateStore(
    var items: MutableMap<String, LockerTemplate>,
    var order: MutableList<String>,
) : LockerTemplateStore {
    override fun find(code: String): LockerTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerTemplate) {
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

    override fun list(): MutableList<LockerTemplate> {
        val out = mutableListOf<LockerTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerTemplateStore(): LockerTemplateStore {
    return MemoryLockerTemplateStore(mutableMapOf<String, LockerTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerTemplateStore(
    var inner: LockerTemplateStore,
    var cache: TtlCache<LockerTemplate>,
    var hits: Int,
    var misses: Int,
) : LockerTemplateStore {
    override fun find(code: String): LockerTemplate? {
        val hit = (cache.get(code) as LockerTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerTemplate> = inner.list()
}

fun newCachedLockerTemplateStore(inner: LockerTemplateStore, ttl: Int, clock: Clock): LockerTemplateStore {
    return CachedLockerTemplateStore(
        inner = inner,
        cache = newTtlCache<LockerTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker templates. Empty fields match all. */
data class LockerTemplateQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerTemplate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerTemplate>): MutableList<LockerTemplate> {
        val matched = filter(items) { candidate: LockerTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerTemplates(store: LockerTemplateStore, query: LockerTemplateQuery): MutableList<LockerTemplate> {
    return query.apply(store.list())
}
