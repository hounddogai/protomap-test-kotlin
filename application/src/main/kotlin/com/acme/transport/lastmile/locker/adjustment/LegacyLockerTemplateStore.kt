package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy locker templates by code. */
interface LegacyLockerTemplateStore {
    fun find(code: String): LegacyLockerTemplate?
    fun save(item: LegacyLockerTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLockerTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLockerTemplateStore(
    var items: MutableMap<String, LegacyLockerTemplate>,
    var order: MutableList<String>,
) : LegacyLockerTemplateStore {
    override fun find(code: String): LegacyLockerTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLockerTemplate) {
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

    override fun list(): MutableList<LegacyLockerTemplate> {
        val out = mutableListOf<LegacyLockerTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLockerTemplateStore(): LegacyLockerTemplateStore {
    return MemoryLegacyLockerTemplateStore(
        mutableMapOf<String, LegacyLockerTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLockerTemplateStore(
    var inner: LegacyLockerTemplateStore,
    var cache: TtlCache<LegacyLockerTemplate>,
    var hits: Int,
    var misses: Int,
) : LegacyLockerTemplateStore {
    override fun find(code: String): LegacyLockerTemplate? {
        val hit = (cache.get(code) as LegacyLockerTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLockerTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLockerTemplate> = inner.list()
}

fun newCachedLegacyLockerTemplateStore(inner: LegacyLockerTemplateStore, ttl: Int, clock: Clock): LegacyLockerTemplateStore {
    return CachedLegacyLockerTemplateStore(
        inner = inner,
        cache = newTtlCache<LegacyLockerTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy locker templates. Empty fields match all. */
data class LegacyLockerTemplateQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLockerTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLockerTemplate>): MutableList<LegacyLockerTemplate> {
        val matched = filter(items) { candidate: LegacyLockerTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLockerTemplates(store: LegacyLockerTemplateStore, query: LegacyLockerTemplateQuery): MutableList<LegacyLockerTemplate> {
    return query.apply(store.list())
}
