package com.acme.transport.customs.declaration.assignment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped declaration templates by code. */
interface CappedDeclarationTemplateStore {
    fun find(code: String): CappedDeclarationTemplate?
    fun save(item: CappedDeclarationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDeclarationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDeclarationTemplateStore(
    var items: MutableMap<String, CappedDeclarationTemplate>,
    var order: MutableList<String>,
) : CappedDeclarationTemplateStore {
    override fun find(code: String): CappedDeclarationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDeclarationTemplate) {
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

    override fun list(): MutableList<CappedDeclarationTemplate> {
        val out = mutableListOf<CappedDeclarationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDeclarationTemplateStore(): CappedDeclarationTemplateStore {
    return MemoryCappedDeclarationTemplateStore(
        mutableMapOf<String, CappedDeclarationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDeclarationTemplateStore(
    var inner: CappedDeclarationTemplateStore,
    var cache: TtlCache<CappedDeclarationTemplate>,
    var hits: Int,
    var misses: Int,
) : CappedDeclarationTemplateStore {
    override fun find(code: String): CappedDeclarationTemplate? {
        val hit = (cache.get(code) as CappedDeclarationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDeclarationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDeclarationTemplate> = inner.list()
}

fun newCachedCappedDeclarationTemplateStore(inner: CappedDeclarationTemplateStore, ttl: Int, clock: Clock): CappedDeclarationTemplateStore {
    return CachedCappedDeclarationTemplateStore(
        inner = inner,
        cache = newTtlCache<CappedDeclarationTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped declaration templates. Empty fields match all. */
data class CappedDeclarationTemplateQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDeclarationTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDeclarationTemplate>): MutableList<CappedDeclarationTemplate> {
        val matched = filter(items) { candidate: CappedDeclarationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDeclarationTemplates(store: CappedDeclarationTemplateStore, query: CappedDeclarationTemplateQuery): MutableList<CappedDeclarationTemplate> {
    return query.apply(store.list())
}
